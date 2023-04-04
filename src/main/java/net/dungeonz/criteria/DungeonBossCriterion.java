package net.dungeonz.criteria;

import com.google.gson.JsonObject;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class DungeonBossCriterion extends AbstractCriterion<DungeonBossCriterion.Conditions> {
    static final Identifier ID = new Identifier("dungeonz:dungeon_completion");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended extended, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
        String dungeonType = JsonHelper.getString(jsonObject, "dungeon_type");
        String difficulty = JsonHelper.getString(jsonObject, "difficulty");
        return new Conditions(extended, dungeonType, difficulty);
    }

    public void trigger(ServerPlayerEntity player, String dungeonType, String difficulty) {
        this.trigger(player, conditions -> conditions.matches(player, dungeonType, difficulty));
    }

    class Conditions extends AbstractCriterionConditions {
        private final String dungeonType;
        private final String difficulty;

        public Conditions(EntityPredicate.Extended player, String dungeonType, String difficulty) {
            super(ID, player);
            this.dungeonType = dungeonType;
            this.difficulty = difficulty;
        }

        public boolean matches(ServerPlayerEntity player, String dungeonType, String difficulty) {
            return this.dungeonType.equals(dungeonType) && this.difficulty.equals(difficulty);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.addProperty("dungeon_type", this.dungeonType);
            jsonObject.addProperty("difficulty", this.difficulty);
            return jsonObject;
        }
    }

}
